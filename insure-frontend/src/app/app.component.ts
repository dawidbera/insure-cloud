import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'insure-frontend';
  
  // Quick Quote
  quoteRequest = {
    productCode: 'CAR_INSURANCE',
    customerAge: 25,
    assetValue: 50000
  };
  quoteResult: any = null;
  
  // Issue Policy
  newPolicy = {
    policyNumber: '',
    customerId: 'CUST-123',
    premiumAmount: 0,
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString().split('T')[0]
  };

  // Search
  searchQuery = '';
  searchResults: any[] = [];
  searchDone = false;

  // Data
  policies: any[] = [];
  auditLogs: any[] = [];
  
  loadingQuote = false;
  loadingPolicy = false;
  loadingSearch = false;

  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchPolicies();
    this.fetchAuditLogs();
  }

  calculateQuote() {
    this.loadingQuote = true;
    this.http.post(`${this.apiUrl}/quotes`, this.quoteRequest)
      .subscribe({
        next: (res) => {
          this.quoteResult = res;
          this.loadingQuote = false;
          this.newPolicy.premiumAmount = this.quoteResult.totalPremium;
          this.newPolicy.policyNumber = 'POL-' + Math.random().toString(36).substring(2, 9).toUpperCase();
        },
        error: (err) => {
          console.error('Quote error:', err);
          this.loadingQuote = false;
          alert('Failed to calculate quote.');
        }
      });
  }

  issuePolicy() {
    this.loadingPolicy = true;
    this.http.post(`${this.apiUrl}/policies`, this.newPolicy)
      .subscribe({
        next: (res) => {
          alert('Policy issued! PDF will be ready in S3 in a few seconds.');
          this.loadingPolicy = false;
          this.fetchPolicies();
          this.fetchAuditLogs();
        },
        error: (err) => {
          console.error('Policy error:', err);
          this.loadingPolicy = false;
          alert('Failed to issue policy.');
        }
      });
  }

  searchPolicies() {
    if (!this.searchQuery) return;
    this.loadingSearch = true;
    this.searchDone = false;
    this.http.get<any[]>(`${this.apiUrl}/search/by-number?policyNumber=${this.searchQuery}`)
      .subscribe({
        next: (res) => {
          console.log('Search results from Elasticsearch:', res);
          this.searchResults = res;
          this.loadingSearch = false;
          this.searchDone = true;
        },
        error: (err) => {
          console.error('Search error:', err);
          this.loadingSearch = false;
          this.searchDone = true;
        }
      });
  }

  downloadPdf(policyNumber: string) {
    const url = `${this.apiUrl}/documents/${policyNumber}`;
    window.open(url, '_blank');
  }

  fetchPolicies() {
    this.http.get<any[]>(`${this.apiUrl}/policies`)
      .subscribe({
        next: (res) => this.policies = res,
        error: (err) => console.error('Policies fetch error:', err)
      });
  }

  fetchAuditLogs() {
    this.http.get<any[]>(`${this.apiUrl}/policies/audit-logs`)
      .subscribe({
        next: (res) => this.auditLogs = res.sort((a,b) => b.timestamp - a.timestamp),
        error: (err) => console.error('Audit logs fetch error:', err)
      });
  }
}
