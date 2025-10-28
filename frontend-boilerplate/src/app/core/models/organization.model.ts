export interface Organization {
  id: string;
  tenantId: string;
  name: string;
  stripeCustomerId: string | null;
  subscriptionStatus: 'FREE' | 'PREMIUM';
  ownerUserId: string;
}