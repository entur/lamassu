/**
 * Utility functions for grouping and deduplicating validation errors
 */

export interface GroupedValidationError {
  message: string;
  schemaPath: string;
  normalizedPath: string;
  count: number;
  examplePaths: string[];
}

/**
 * Normalizes a violation path by replacing array indices with [*]
 * Example: #/data/bikes/0/last_reported -> #/data/bikes/[*]/last_reported
 */
function normalizeViolationPath(path: string): string {
  return path.replace(/\/\d+\//g, '/[*]/').replace(/\/\d+$/, '/[*]');
}

/**
 * Normalizes a message by replacing any embedded paths with normalized versions
 * Example: "#/data/bikes/0/last_reported: error" -> "#/data/bikes/[*]/last_reported: error"
 */
function normalizeMessage(message: string): string {
  // Replace any paths found in the message (handles paths like #/data/bikes/0/field)
  return message.replace(/#?\/[^\s:]+\/\d+\/[^\s:]*/g, match => {
    return normalizeViolationPath(match);
  });
}

/**
 * Groups similar validation errors together to avoid rendering thousands of identical errors.
 * Errors are grouped by their normalized path, message, and schema path.
 */
export function groupValidationErrors(
  errors: Array<{ message: string; schemaPath: string; violationPath: string }>
): GroupedValidationError[] {
  const groupMap = new Map<string, GroupedValidationError>();

  errors.forEach(error => {
    const normalizedPath = normalizeViolationPath(error.violationPath);
    const normalizedMsg = normalizeMessage(error.message);
    const key = `${normalizedPath}||${normalizedMsg}||${error.schemaPath}`;

    const existing = groupMap.get(key);
    if (existing) {
      existing.count++;
      // Keep up to 5 example paths
      if (existing.examplePaths.length < 5) {
        existing.examplePaths.push(error.violationPath);
      }
    } else {
      groupMap.set(key, {
        message: normalizedMsg,
        schemaPath: error.schemaPath,
        normalizedPath,
        count: 1,
        examplePaths: [error.violationPath],
      });
    }
  });

  // Sort by count (descending) so most common errors appear first
  return Array.from(groupMap.values()).sort((a, b) => b.count - a.count);
}
