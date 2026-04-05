"""
generate_purchase_order.py
--------------------------
Reads all pending prescriptions from the DB, aggregates medicines by name
(summing quantities where parseable), generates a simple PDF purchase order,
then marks the prescriptions as ordered.

Layers:
  Models      — AggregatedMedicine, PurchaseOrder
  Repository  — DB reads/writes
  Aggregator  — groups and sums medicines across prescriptions
  PDFGenerator — renders a simple table PDF via reportlab
  Pipeline    — orchestrates fetch → aggregate → generate → mark ordered
"""

from __future__ import annotations

import os
import sys
import uuid
from dataclasses import dataclass, field
from datetime import date, datetime
from typing import Optional

import psycopg2
from psycopg2.extras import RealDictCursor
from dotenv import load_dotenv
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, HRFlowable
)

load_dotenv()


# ==============================================================
# Models
# ==============================================================

@dataclass
class AggregatedMedicine:
    """A medicine aggregated across all pending prescriptions."""
    name:           str
    total_quantity: Optional[int]   # sum of quantity fields; None if all were null
    raw_dosages:    list[str] = field(default_factory=list)
    patient_count:  int = 0


@dataclass
class PurchaseOrder:
    order_number:     str
    generated_at:     datetime
    medicines:        list[AggregatedMedicine]
    prescription_ids: list[int]

    @classmethod
    def create(cls, medicines: list[AggregatedMedicine], prescription_ids: list[int]) -> PurchaseOrder:
        order_number = f"PO-{date.today().strftime('%Y%m%d')}-{str(uuid.uuid4())[:6].upper()}"
        return cls(
            order_number=order_number,
            generated_at=datetime.now(),
            medicines=medicines,
            prescription_ids=prescription_ids,
        )


# ==============================================================
# Repository
# ==============================================================

class PurchaseOrderRepository:
    """Handles all DB operations needed for purchase order generation."""

    _GET_PENDING = """
        SELECT
            p.id                AS prescription_id,
            m.name              AS medicine_name,
            m.dosage,
            m.quantity
        FROM prescriptions p
        JOIN prescription_medicines m ON m.prescription_id = p.id
        WHERE p.status = 'pending'
        ORDER BY lower(m.name);
    """

    _MARK_ORDERED = """
        UPDATE prescriptions
        SET status = 'ordered'
        WHERE id = ANY(%s);
    """

    def __init__(self, cursor):
        self.cursor = cursor

    def get_pending_rows(self) -> list[dict]:
        self.cursor.execute(self._GET_PENDING)
        return self.cursor.fetchall()

    def mark_ordered(self, prescription_ids: list[int]):
        self.cursor.execute(self._MARK_ORDERED, (prescription_ids,))
        print(f"Marked {self.cursor.rowcount} prescription(s) as ordered.")


# ==============================================================
# Aggregator
# ==============================================================

class MedicineAggregator:
    """Groups medicines by name and sums quantities across prescriptions."""

    def aggregate(self, rows: list[dict]) -> tuple[list[AggregatedMedicine], list[int]]:
        medicines:        dict[str, dict] = {}
        prescription_ids: set[int]        = set()

        for row in rows:
            name = (row["medicine_name"] or "Unknown").strip()
            prescription_ids.add(row["prescription_id"])

            if name not in medicines:
                medicines[name] = {
                    "total_quantity": None,
                    "raw_dosages":    [],
                    "patient_count":  0,
                }

            bucket = medicines[name]
            bucket["patient_count"] += 1

            if row["dosage"]:
                bucket["raw_dosages"].append(row["dosage"])

            if row["quantity"] is not None:
                bucket["total_quantity"] = (bucket["total_quantity"] or 0) + row["quantity"]

        aggregated = [
            AggregatedMedicine(
                name=name,
                total_quantity=b["total_quantity"],
                raw_dosages=b["raw_dosages"],
                patient_count=b["patient_count"],
            )
            for name, b in medicines.items()
        ]

        return sorted(aggregated, key=lambda m: m.name), sorted(prescription_ids)


# ==============================================================
# PDF Generator
# ==============================================================

class PurchaseOrderPDFGenerator:
    """Renders a simple purchase order table as a PDF."""

    BRAND_BLUE = colors.HexColor("#1a3c5e")
    LIGHT_BLUE = colors.HexColor("#e8f0f7")
    MED_GREY   = colors.HexColor("#6b7280")
    WHITE      = colors.white

    def __init__(self, output_path: str):
        self.output_path = output_path

    def generate(self, po: PurchaseOrder):
        doc = SimpleDocTemplate(
            self.output_path,
            pagesize=A4,
            leftMargin=20*mm, rightMargin=20*mm,
            topMargin=18*mm, bottomMargin=18*mm,
        )

        story = [
            Paragraph("PURCHASE ORDER", ParagraphStyle(
                "Title", fontSize=18, fontName="Helvetica-Bold",
                textColor=self.BRAND_BLUE, spaceAfter=1,
            )),
            Paragraph(
                f"Order No: {po.order_number} &nbsp;&nbsp;·&nbsp;&nbsp; "
                f"Date: {po.generated_at.strftime('%d %b %Y')} &nbsp;&nbsp;·&nbsp;&nbsp; "
                f"Prescriptions: {len(po.prescription_ids)}",
                ParagraphStyle("Meta", fontSize=9, fontName="Helvetica",
                               textColor=self.MED_GREY),
            ),
            Spacer(1, 3*mm),
            HRFlowable(width="100%", thickness=1.5, color=self.BRAND_BLUE),
            Spacer(1, 5*mm),
            self._build_table(po.medicines),
            Spacer(1, 3*mm),
            Paragraph(
                "* Quantity could not be parsed from dosage string — verify manually.",
                ParagraphStyle("Note", fontSize=7.5, fontName="Helvetica-Oblique",
                               textColor=self.MED_GREY),
            ),
            Spacer(1, 10*mm),
            HRFlowable(width="100%", thickness=0.5, color=self.MED_GREY),
            Spacer(1, 2*mm),
            Paragraph(
                f"Generated by Prescription Management System · "
                f"{po.generated_at.strftime('%d %b %Y, %I:%M %p')}",
                ParagraphStyle("Footer", fontSize=7, fontName="Helvetica",
                               textColor=self.MED_GREY, alignment=1),
            ),
        ]

        doc.build(story)
        print(f"PDF saved to: {self.output_path}")

    def _build_table(self, medicines: list[AggregatedMedicine]):
        header = ["#", "Medicine", "Total Qty", "Patients", "Raw Dosages"]
        rows = [header]

        for i, med in enumerate(medicines, 1):
            qty_str = str(med.total_quantity) if med.total_quantity is not None else "— *"
            unique_dosages = sorted(set(med.raw_dosages))
            rows.append([
                str(i),
                med.name,
                qty_str,
                str(med.patient_count),
                ", ".join(unique_dosages) or "—",
            ])

        table = Table(rows, colWidths=[10*mm, 60*mm, 30*mm, 22*mm, 48*mm], repeatRows=1)
        table.setStyle(TableStyle([
            ("BACKGROUND",    (0, 0), (-1, 0),  self.BRAND_BLUE),
            ("TEXTCOLOR",     (0, 0), (-1, 0),  self.WHITE),
            ("FONTNAME",      (0, 0), (-1, 0),  "Helvetica-Bold"),
            ("FONTSIZE",      (0, 0), (-1, 0),  9),
            ("FONTNAME",      (0, 1), (-1, -1), "Helvetica"),
            ("FONTSIZE",      (0, 1), (-1, -1), 8),
            ("ROWBACKGROUNDS",(0, 1), (-1, -1), [self.WHITE, self.LIGHT_BLUE]),
            ("ALIGN",         (0, 0), (0, -1),  "CENTER"),
            ("ALIGN",         (2, 0), (3, -1),  "CENTER"),
            ("VALIGN",        (0, 0), (-1, -1), "MIDDLE"),
            ("GRID",          (0, 0), (-1, -1), 0.4, colors.HexColor("#d1d5db")),
            ("TOPPADDING",    (0, 0), (-1, -1), 5),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
            ("LEFTPADDING",   (0, 0), (-1, -1), 6),
        ]))
        return table


# ==============================================================
# Pipeline
# ==============================================================

class PurchaseOrderPipeline:
    """Orchestrates: fetch pending → aggregate → generate PDF → mark ordered."""

    def __init__(self, conn, output_path: str):
        self.conn = conn
        self.output_path = output_path

    def run(self):
        with self.conn:
            with self.conn.cursor(cursor_factory=RealDictCursor) as cur:
                repo = PurchaseOrderRepository(cur)

                rows = repo.get_pending_rows()
                if not rows:
                    print("No pending prescriptions found. Nothing to do.")
                    return

                print(f"Found {len(rows)} medicine row(s) across pending prescriptions.")

                medicines, prescription_ids = MedicineAggregator().aggregate(rows)
                print(f"Aggregated into {len(medicines)} unique medicine(s) "
                      f"from {len(prescription_ids)} prescription(s).")

                po = PurchaseOrder.create(medicines, prescription_ids)
                PurchaseOrderPDFGenerator(self.output_path).generate(po)
                repo.mark_ordered(prescription_ids)

                print(f"Purchase order {po.order_number} complete.")


# ==============================================================
# Entrypoint
# ==============================================================

def main():
    conn_string = os.environ["DATABASE_URL"]
    output_path = sys.argv[1] if len(sys.argv) > 1 else f"purchase_order_{date.today().isoformat()}.pdf"

    conn = psycopg2.connect(conn_string)
    try:
        PurchaseOrderPipeline(conn, output_path).run()
    finally:
        conn.close()


if __name__ == "__main__":
    main()