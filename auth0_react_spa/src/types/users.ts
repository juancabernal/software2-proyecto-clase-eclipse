export type User = {
  userId: string;
  idType: string;
  idNumber: string;
  fullName: string;
  email: string;
  mobileNumber?: string;
  emailConfirmed: boolean;
  mobileNumberConfirmed: boolean;
};
