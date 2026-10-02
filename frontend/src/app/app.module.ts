import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {MatSnackBarModule} from '@angular/material/snack-bar';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {MapOverviewComponent} from "./components/map-overview/map-overview.component";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {SignupComponent} from "./components/signup/signup.component";
import {FarmerEditComponent} from "./components/farmer/edit/farmer-edit.component";
import {FarmerDetailsComponent} from "./components/farmer/details/farmer-details.component";
import {
  FarmerChangePasswordComponent
} from "./components/farmer/farmer-change-password/farmer-change-password.component";
import {CropTypeComponent} from "./components/crop-type/crop-type/crop-type.component";
import {CropTypeDetailComponent} from "./components/crop-type/crop-type-detail/crop-type-detail-component";
import {CropTypeEditComponent} from "./components/crop-type/crop-type-edit/crop-type-edit.component";
import {SeedComponent} from "./components/seed/seed/seed.component";
import {SeedDetailComponent} from "./components/seed/seed-detail/seed-detail.component";
import {SeedEditComponent} from "./components/seed/seed-edit/seed-edit.component";
import {MapComponent} from "./components/shared/map/map.component";
import {SchedulingComponent} from "./components/scheduling/scheduling.component";
import {EquipmentComponent} from "./components/equipment/equipment/equipment.component";
import {EquipmentDetailComponent} from "./components/equipment/equipment-detail/equipment-detail.component";
import {EquipmentEditComponent} from "./components/equipment/equipment-edit/equipment-edit.component";
import {FieldDetailsComponent} from "./components/field/field-details/field-details.component";
import {FieldCreateEditComponent} from "./components/field/field-create-edit/field-create-edit.component";
import {PlantSeedComponent} from "./components/field/plant-seed/plant-seed.component";
import {InfoFieldComponent} from "./components/shared/info-field/info-field.component";
import {PanelLayoutComponent} from "./components/shared/panel-layout/panel-layout.component";
import {EditFieldComponent} from "./components/shared/edit-field/edit-field.component";
import {FieldDataComponent} from "./components/data-visualisation/field-data/field-data.component";
import {FarmDataComponent} from "./components/data-visualisation/farm-data/farm-data.component";
import {MatSortModule} from '@angular/material/sort';
import {MatTableModule} from '@angular/material/table';
import {HarvestFieldComponent} from "./components/field/harvest-field/harvest-field.component";
import {GarageComponent} from "./components/garage/garage/garage.component";
import {GarageEditComponent} from "./components/garage/garage-edit/garage-edit.component";
import {GarageDetailComponent} from "./components/garage/garage-detail/garage-detail.component";
import {AddressAutocompleteComponent} from "./components/shared/address-autocomplete/address-autocomplete.component";
import {MatTooltip} from "@angular/material/tooltip";
import {MapPanelLayoutComponent} from "./components/shared/map-panel-layout/map-panel-layout.component";
import {GarageFormComponent} from "./components/garage/garage-form/garage-form.component";
import {ConfirmationDialogComponent} from "./components/shared/confirmation-dialog/confirmation-dialog.component";
import {
  DashboardListBaseComponent
} from "./components/list-dashboards/dashboard-list-base/dashboard-list-base.component";
import {EquipmentListComponent} from "./components/list-dashboards/equipment-list/equipment-list.component";
import {EntityListComponent} from "./components/list-dashboards/entity-list/entity-list.component";
import {SeedListComponent} from "./components/list-dashboards/seed-list/seed-list.component";
import {CropListComponent} from "./components/list-dashboards/crop-list/crop-list.component";
import {GarageListComponent} from "./components/list-dashboards/garage-list/garage-list.component";
import {MatPaginatorModule} from '@angular/material/paginator';
import {ToolCategoryListComponent} from "./components/list-dashboards/tool-category-list/tool-category-list.component";
import {CropTypeListComponent} from "./components/list-dashboards/crop-type-list/crop-type-list.component";
import {TaskDetailComponent} from "./components/scheduling/task/task-detail/task-detail.component";
import {MatDialogModule} from "@angular/material/dialog";
import AddTaskComponent from "./components/scheduling/task/add-task/add-task/add-task.component";
import {MatNativeDateModule, MatOptionModule} from "@angular/material/core";
import {MatInputModule} from "@angular/material/input";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {EditTaskComponent} from "./components/scheduling/task/edit-task/edit-task/edit-task.component";
import {ActiveFieldListComponent} from "./components/list-dashboards/active-field-list/active-field-list.component";
import {FieldListComponent} from "./components/list-dashboards/field-list/field-list.component";
import {ToastrModule} from "ngx-toastr";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MultiSelectModule} from 'primeng/multiselect';
import {
  EquipmentSetLocationComponent
} from "./components/equipment/equipment-set-location/equipment-set-location.component";
import {providePrimeNG} from 'primeng/config';
import Aura from '@primeng/themes/aura';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {ToolSelector} from "./components/scheduling/task/tool-selector/tool-selector";

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    CropTypeComponent,
    CropTypeDetailComponent,
    CropTypeEditComponent,
    SchedulingComponent,
    EquipmentComponent,
    EquipmentDetailComponent,
    EquipmentEditComponent,
    SeedComponent,
    SeedDetailComponent,
    SeedEditComponent,
    LoginComponent,
    MapOverviewComponent,
    FarmerEditComponent,
    FarmerDetailsComponent,
    FarmerChangePasswordComponent,
    SignupComponent,
    TaskDetailComponent,
    AddTaskComponent,
    MapComponent,
    FieldDetailsComponent,
    FieldCreateEditComponent,
    PlantSeedComponent,
    HarvestFieldComponent,
    FieldDataComponent,
    FarmDataComponent,
    HarvestFieldComponent,
    GarageFormComponent,
    MapPanelLayoutComponent,
    EditTaskComponent,
    GarageComponent,
    GarageEditComponent,
    GarageDetailComponent,
    DashboardListBaseComponent,
    EquipmentListComponent,
    SeedListComponent,
    CropListComponent,
    ActiveFieldListComponent,
    FieldListComponent,
    GarageListComponent,
    ToolCategoryListComponent,
    CropTypeListComponent,
    GarageListComponent,
    EquipmentListComponent,
    EntityListComponent,
    EquipmentSetLocationComponent
  ],
  bootstrap: [AppComponent],
  imports: [BrowserModule,
    MatSnackBarModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot(),
    AppRoutingModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatOptionModule,
    MatNativeDateModule,
    InfoFieldComponent,
    PanelLayoutComponent,
    EditFieldComponent,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    ConfirmationDialogComponent,
    AddressAutocompleteComponent,
    MultiSelectModule,
    MatTooltip,
    MatSlideToggleModule, ToolSelector,
  ],
  exports: [
    MapComponent,
    MapPanelLayoutComponent,
    EntityListComponent,
    GarageFormComponent,
  ],
  providers: [httpInterceptorProviders, provideHttpClient(withInterceptorsFromDi()),
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: false,
        }
      }
    })]
})
export class AppModule {
}
