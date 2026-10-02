export interface InfoField {
  label: string;
  value: string | number | null;
}

export interface EditField {
  label: string;
  key: string;
  type: 'text' | 'number' | 'email' | 'password' | 'date';
  placeholder?: string;
  errorMessage?: string;
}

export interface ActionButton {
  label: string;
  color: 'primary' | 'warn' | 'accent' | 'default' | 'secondary';
  action: () => void;
}

// action for the back button
export interface BackAction {
  action: () => void;
}


// Config for detail/ info pages
export interface DetailConfig<T = any> {
  title: string;
  subtitle: string;
  actionButtons: ActionButton[];
  backAction?: BackAction;
  fields: InfoField[];
  data?: T;
}

// Config for edit/create forms/pages
export interface FormLayoutConfig<T = any> {
  title: string;
  subtitle: string;
  actionButtons: ActionButton[];
  backAction?: BackAction;
  fields: EditField[];
  data?: T;
}
