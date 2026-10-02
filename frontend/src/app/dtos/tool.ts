export interface ToolCategory {
  id: number | null;
  name: string;

}

export interface Garage {
  id: number | null;
  name: string;
  ownerId: number;
  coordinate: Location | null;
  tools?: Tool[] | null;
}

export interface Location {
  lat: number;
  lon: number;
}

export interface Tool {
  id: number | null;
  name: string;
  categoryDto: ToolCategory | null;
  garageDto: Garage | null;
  locationDto: Location | null;
  description: string;
  remainingValue: number;
  usable: boolean;
  image: string | null;
}

export interface DropdownTool {
  id: number;
  name: string;
  raw: Tool;
}

export interface DropdownCategory {
  id: number;
  name: string;
  tools: DropdownTool[];
  rawCategory: ToolCategory | null;
}

export function mapToolsToDropdownCategories(
  tools: Tool[],
  options?: { includeUncategorized?: boolean; uncategorizedName?: string }
): DropdownCategory[] {

  console.log(tools);

  const includeUncategorized = options?.includeUncategorized ?? true;
  const uncategorizedName = options?.uncategorizedName ?? 'Uncategorized';

  const UNC_KEY = '__uncategorized__';

  const map = new Map<string, DropdownCategory>();

  for (const tool of tools) {
    const cat = tool.categoryDto;

    const key =
      cat?.id != null ? String(cat.id) : includeUncategorized ? UNC_KEY : null;

    if (key === null) continue;

    if (!map.has(key)) {
      map.set(key, {
        id: key === UNC_KEY ? -1 : cat!.id,
        name: key === UNC_KEY ? uncategorizedName : cat!.name,
        tools: [],
        rawCategory: key === UNC_KEY ? null : cat!,
      });
    }

    map.get(key)!.tools.push({
      id: tool.id != null ? tool.id : -1,
      name: tool.name,
      raw: tool,
    });
  }

  // Optional: sort categories and tools alphabetically
  const result = Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
  for (const c of result) c.tools.sort((a, b) => a.name.localeCompare(b.name));

  return result;
}

