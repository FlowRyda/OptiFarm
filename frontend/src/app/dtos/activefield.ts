export interface SimpleActiveFieldDto {
  id: number;
  name?: string;
  growthStatus: number;
  fieldId: number;
}

export interface ActiveFieldCreateDto {
  startDate: Date;
  cropTypeId: number;
  fieldId: number;
  sellingPrice: number;
  expectedHarvestDate: Date;
}

export interface ActiveFieldDetailDto {
  id: number;
  name?: string;
  plantedDate: Date;
  expectedDate: Date;
  harvestDate: Date;
  sellingPrice: number;
  growthStatus: number;
  cropTypeId: number;
  fieldId: number;
  farmerId: number;
  seedName: string;
  seedPrice: number;
}

export interface ActiveFieldUpdateDto {
  id: number;
  startDate: Date;
  endDate: Date;
  expectedDate: Date,
  cropTypeId: number;
  fieldId: number;
}
