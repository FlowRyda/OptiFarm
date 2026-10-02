import {EditField} from '../../shared/panel-layout/data';

export const GARAGE_EDIT_FIELDS: EditField[] = [
  {
    label: 'Name',
    key: 'name',
    type: 'text',
    placeholder: 'Enter name of the Garage (e.g., Westfield, ...)',
    errorMessage: 'Name is required.'
  }
];
