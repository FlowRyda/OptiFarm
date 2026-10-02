import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import { Router } from "@angular/router";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
    standalone: false
})
export class HomeComponent implements OnInit {

  constructor(public authService: AuthService, private router: Router) { }

  ngOnInit() {
    if(this.authService.isLoggedIn()){
      this.router.navigate(['/map-overview']);
    }
  }

}
