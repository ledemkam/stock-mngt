import {ValidationError} from './validation-error';

export interface ErrorResponse {
  code?: string;
  message?: string;
  path?: string;
  validationErrors?: Array<ValidationError>;
}
