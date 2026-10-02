import { Component } from '@angular/core';
import { Router } from "@angular/router";

interface NavItem {
  label: string;
  icon: string;       // swap to real icons later
  route: string;
}

@Component({
  selector: 'app-dashboard-list-base',
  templateUrl: './dashboard-list-base.component.html',
  standalone: false,
  styleUrl: './dashboard-list-base.component.scss'
})
export class DashboardListBaseComponent {
  activeType = 'equipment';

  navItems: NavItem[] = [
    { label: 'Field',         icon: '🟫', route: 'field' },
    { label: 'Active Field',         icon: '🟩', route: 'activeField' },
    { label: 'Crop',          icon: '🌾', route: 'crop' },
    { label: 'Crop Type',          icon: '🌱', route: 'cropType' },
    { label: 'Seed',          icon: '🫘', route: 'seed' },
    { label: 'Equipment',     icon: '🛠', route: 'equipment' },
    { label: 'Tool Category',     icon: '🧰', route: 'toolCategory' },
    { label: 'Garage',        icon: '🏠', route: 'garage' },
  ];

  constructor(private router: Router) {}

  navigate(route: string) {
    this.activeType = route;
    this.router.navigate(['dashboard', route]);
  }
}
