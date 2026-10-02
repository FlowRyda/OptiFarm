export class ChangePasswordDto {
  constructor(
    public newPassword: string,
    public oldPassword: string
  ) {}
}
