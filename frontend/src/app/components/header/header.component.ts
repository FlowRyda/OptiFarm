import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';

interface NavigationItem {
  label: string;
  route: string;
}

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
    standalone: false
})
export class HeaderComponent implements OnInit {

  navigationItems: NavigationItem[] = [
    { label: 'Overview', route: '/map-overview' },
    { label: 'Equipment Scheduling', route: '/scheduling' },
    { label: 'Data Analysis', route: '/farm/data' },
    { label: 'Dashboard', route: '/dashboard' }
  ];

  constructor(public authService: AuthService) { }

  ngOnInit() {
  }

}
