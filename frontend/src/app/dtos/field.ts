export interface SimpleFieldDto {
  id: number;
  name?: string;
  boundaries: CoordinateDto[];
  exclusions?: CoordinateDto[][];
}

export interface FieldCreateDto {
  name?: string;
  boundaries?: CoordinateDto[];
  exclusions?: CoordinateDto[][];
}

export interface FieldUpdateDto {
  name?: string;
  boundaries?: CoordinateDto[];
  exclusions?: CoordinateDto[][];
}

export interface FieldDetailDto {
  id: number;
  name?: string;
  cropName: string;
  cropPrice: number;
  cropProgress: number;
  boundaries: CoordinateDto[];
  exclusions?: CoordinateDto[][];
}

export interface CoordinateDto {
  lat: number;
  lon: number;
}

export interface FieldValidationRequestDto {
  boundaries: CoordinateDto[];
  exclusions?: CoordinateDto[][];
  excludeFieldId?: number;
}

export interface FieldValidationResponseDto {
  valid: boolean;
  errorMessage?: string;
  errorCoordinates?: CoordinateDto[];
  overlappingFieldName?: string;
}
