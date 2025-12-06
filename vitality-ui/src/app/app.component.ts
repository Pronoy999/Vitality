import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PatientInfoFormComponent } from './patient-info-form/patient-info-form.component';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, PatientInfoFormComponent],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})

export class AppComponent {
    title = 'vitality-ui';
}