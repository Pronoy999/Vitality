import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MaterialModule } from '../shared/material.module';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-patient-info-form',
    imports: [FormsModule, ReactiveFormsModule, CommonModule, MaterialModule],
    templateUrl: './patient-info-form.component.html',
    styleUrls: ['./patient-info-form.component.scss']
})

export class PatientInfoFormComponent {
    patientInfoForm: FormGroup;

    constructor(private fb: FormBuilder, private http: HttpClient) {
        this.patientInfoForm = this.fb.group({
            name: ['', Validators.required],
            phoneNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
            email: ['', [Validators.email]],
            age: ['', [Validators.required, Validators.min(0)]],
            height: ['', [Validators.min(0)]],
            weight: ['', [Validators.min(0)]],
            gender: [''],
            bloodPressure: [''],
            ailmentHistory: [''],
            hasHealthInsurance: [false],
            abhaId: [''],
            additionalDiagnosis: [''],
            medicinesConsumed: [''],
            additionalServicesRequired: ['']
        });
    }

    patientInfoFormSubmit() {
        if (this.patientInfoForm.valid) {
            console.log('Patient Information:', this.patientInfoForm.value);
        } else {
            console.log('Form is invalid');
        }

        this.httpPostPatientInfo(this.patientInfoForm.value);
    }

    private httpPostPatientInfo(patientData: any): void {
        const url = 'http://122.166.244.91:8080/api/v1/vitality/patient';
        this.http.post(url, patientData).subscribe(
            (response) => {
                console.log('POST request successful:', response);
                // Handle the successful response
            },
            (error) => {
                console.error('POST request failed:', error);
                // Handle the error
            }
        );
    }
}
