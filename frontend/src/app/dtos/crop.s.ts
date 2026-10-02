export interface CropCreateDto {
  farmerId: number,
  cropTypeId: number,
  currentPrice: number,
  harvestDate: Date,
  quantity: number,
  fieldId: number
}

export interface CropDetailDto {
  id: number,
  currentPrice: number,
  harvestDate: Date,
  quantity: number,
  farmerId: number,
  cropTypeId: number
}

export interface CropDto {
  id: number,
  currentPrice: number,
  harvestDate: Date,
  quantity: number,
  cropType: string
}
