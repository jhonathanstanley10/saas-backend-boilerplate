export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
}