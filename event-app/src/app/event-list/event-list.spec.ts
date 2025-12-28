import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing'; // Ajout nécessaire pour le Service
// Correction 1 : On importe le bon nom de fichier et de classe
import { EventListComponent } from './event-list';

describe('EventListComponent', () => {
  let component: EventListComponent;
  let fixture: ComponentFixture<EventListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // Correction 2 : On importe le composant Standalone ici
      imports: [EventListComponent, HttpClientTestingModule]
    })
      .compileComponents();

    fixture = TestBed.createComponent(EventListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
