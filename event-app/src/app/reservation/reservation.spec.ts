import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReservationComponent } from './reservation';

describe('Reservation', () => {
  let component: ReservationComponent;
  let fixture: ComponentFixture<ReservationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReservationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReservationComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
