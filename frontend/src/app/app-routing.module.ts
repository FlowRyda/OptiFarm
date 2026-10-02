import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {EquipmentComponent} from './components/equipment/equipment/equipment.component';
import {SignupComponent} from "./components/signup/signup.component";
import {FarmerDetailsComponent} from "./components/farmer/details/farmer-details.component";
import {FarmerEditComponent} from "./components/farmer/edit/farmer-edit.component";
import {
  FarmerChangePasswordComponent
} from "./components/farmer/farmer-change-password/farmer-change-password.component";
import {MapOverviewComponent} from "./components/map-overview/map-overview.component";
import {FarmerSplitComponent} from "./components/farmer/farmer-split/farmer-split.component";
import {CropTypeComponent} from "./components/crop-type/crop-type/crop-type.component";
import {CropTypeDetailComponent} from "./components/crop-type/crop-type-detail/crop-type-detail-component";
import {CropTypeEditComponent} from "./components/crop-type/crop-type-edit/crop-type-edit.component";
import {SeedComponent} from "./components/seed/seed/seed.component";
import {SeedDetailComponent} from "./components/seed/seed-detail/seed-detail.component";
import {SeedEditComponent} from "./components/seed/seed-edit/seed-edit.component";
import {SchedulingComponent} from "./components/scheduling/scheduling.component";
import {FieldDetailsComponent} from "./components/field/field-details/field-details.component";
import {
  FieldCreateEditComponent
} from "./components/field/field-create-edit/field-create-edit.component";
import {PlantSeedComponent} from "./components/field/plant-seed/plant-seed.component";
import {HarvestFieldComponent} from "./components/field/harvest-field/harvest-field.component";
import {EquipmentDetailComponent} from "./components/equipment/equipment-detail/equipment-detail.component";
import {EquipmentEditComponent} from "./components/equipment/equipment-edit/equipment-edit.component";
import {FieldDataComponent} from "./components/data-visualisation/field-data/field-data.component";
import {FarmDataComponent} from "./components/data-visualisation/farm-data/farm-data.component";
import {GarageComponent} from "./components/garage/garage/garage.component";
import {GarageEditComponent} from "./components/garage/garage-edit/garage-edit.component";
import {GarageDetailComponent} from "./components/garage/garage-detail/garage-detail.component";
import {
  DashboardListBaseComponent
} from "./components/list-dashboards/dashboard-list-base/dashboard-list-base.component";
import {EquipmentListComponent} from "./components/list-dashboards/equipment-list/equipment-list.component";
import {GarageListComponent} from "./components/list-dashboards/garage-list/garage-list.component";
import {CropListComponent} from "./components/list-dashboards/crop-list/crop-list.component";
import {SeedListComponent} from "./components/list-dashboards/seed-list/seed-list.component";
import {ToolCategoryListComponent} from "./components/list-dashboards/tool-category-list/tool-category-list.component";
import {CropTypeListComponent} from "./components/list-dashboards/crop-type-list/crop-type-list.component";
import {ActiveFieldListComponent} from "./components/list-dashboards/active-field-list/active-field-list.component";
import {FieldListComponent} from "./components/list-dashboards/field-list/field-list.component";
import {
  EquipmentSetLocationComponent
} from "./components/equipment/equipment-set-location/equipment-set-location.component";

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'signup', component: SignupComponent},
  {path: 'crop-types/:id/edit', canActivate: mapToCanActivate([AuthGuard]), component: CropTypeEditComponent},
  {path: 'cropType/:id', canActivate: mapToCanActivate([AuthGuard]), component: CropTypeDetailComponent},
  {path: 'cropType', canActivate: mapToCanActivate([AuthGuard]), component: CropTypeComponent},
  {path: 'seed/:id/edit', canActivate: mapToCanActivate([AuthGuard]), component: SeedEditComponent},
  {path: 'seed/:id', canActivate: mapToCanActivate([AuthGuard]), component: SeedDetailComponent},
  {path: 'seed', canActivate: mapToCanActivate([AuthGuard]), component: SeedComponent},
  {path: 'scheduling', canActivate: mapToCanActivate([AuthGuard]), component: SchedulingComponent},
  {
    path: 'farmer',
    component: FarmerSplitComponent,
    children: [
      {path: '', redirectTo: 'details', pathMatch: 'full'},
      {path: 'details', canActivate: mapToCanActivate([AuthGuard]), component: FarmerDetailsComponent},
      {path: 'edit', canActivate: mapToCanActivate([AuthGuard]), component: FarmerEditComponent},
      {path: 'password', canActivate: mapToCanActivate([AuthGuard]), component: FarmerChangePasswordComponent},
    ],
  },
  {
    path: 'garage',
    children: [
      {path: '', redirectTo: 'create', pathMatch: 'full'},
      {path: 'details/:id', canActivate: mapToCanActivate([AuthGuard]), component: GarageDetailComponent},
      {path: 'edit/:id', canActivate: mapToCanActivate([AuthGuard]), component: GarageEditComponent},
      {path: 'create', canActivate: mapToCanActivate([AuthGuard]), component: GarageComponent},
    ],
  },
  {path: 'field/create', canActivate: mapToCanActivate([AuthGuard]), component: FieldCreateEditComponent},
  {path: 'field/edit/:id', canActivate: mapToCanActivate([AuthGuard]), component: FieldCreateEditComponent},
  {path: 'field/details/:id', canActivate: mapToCanActivate([AuthGuard]), component: FieldDetailsComponent},
  {path: 'field/plant-seed/:id', canActivate: mapToCanActivate([AuthGuard]), component: PlantSeedComponent},
  {path: 'activeField/harvestPage/:id', canActivate: mapToCanActivate([AuthGuard]), component: HarvestFieldComponent},
  {path: 'field/data/:id', canActivate: mapToCanActivate([AuthGuard]), component: FieldDataComponent},
  {path: 'farm/data', canActivate: mapToCanActivate([AuthGuard]), component: FarmDataComponent},

  {path: 'map-overview', canActivate: mapToCanActivate([AuthGuard]), component: MapOverviewComponent},
  {
    path: 'equipment/create',
    canActivate: mapToCanActivate([AuthGuard]),
    component: EquipmentComponent,
    data: {isVehicle: false}
  },
  {
    path: 'equipment/edit/:id',
    canActivate: mapToCanActivate([AuthGuard]),
    component: EquipmentEditComponent,
    data: {isVehicle: false}
  },
  {
    path: 'equipment/detail/:id',
    canActivate: mapToCanActivate([AuthGuard]),
    component: EquipmentDetailComponent,
    data: {isVehicle: false}
  },
  {
    path: 'equipment/:id/set-position/:token',
    component: EquipmentSetLocationComponent
  },
  {
    path: 'vehicle/create',
    canActivate: mapToCanActivate([AuthGuard]),
    component: EquipmentComponent,
    data: {isVehicle: true}
  },
  {
    path: 'vehicle/edit/:id',
    canActivate: mapToCanActivate([AuthGuard]),
    component: EquipmentEditComponent,
    data: {isVehicle: true}
  },
  {
    path: 'vehicle/detail/:id',
    canActivate: mapToCanActivate([AuthGuard]),
    component: EquipmentDetailComponent,
    data: {isVehicle: true}
  },
  {
    path: 'dashboard',
    canActivate: mapToCanActivate([AuthGuard]),
    component: DashboardListBaseComponent,
    children: [
      {path: 'equipment', component: EquipmentListComponent},
      {path: 'activeField', component: ActiveFieldListComponent},
      {path: 'field', component: FieldListComponent},
      {path: 'garage', component: GarageListComponent},
      {path: 'crop', component: CropListComponent},
      {path: 'seed', component: SeedListComponent},
      {path: 'toolCategory', component: ToolCategoryListComponent},
      {path: 'cropType', component: CropTypeListComponent},
      {path: '', redirectTo: 'equipment', pathMatch: 'full'}  // default
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
