type FileValidationError = {
  schemaPath: string;
  violationPath: string;
  message: string;
};

type ShortFileValidationResult = {
  file: string;
  required: boolean;
  exists: boolean;
  errorsCount: number;
  version: string;
  errors: FileValidationError[];
};

export type ShortValidationReport = {
  summary: {
    version: string;
    timestamp: number;
    errorsCount: number;
  };
  files: Record<string, ShortFileValidationResult>;
};

export type ValidationReports = Record<string, ShortValidationReport>;
