export function normalizeError(error, fallbackMessage = 'Something went wrong') {
  return {
    status: error?.response?.status ?? 500,
    message: error?.response?.data?.message ?? error?.message ?? fallbackMessage
  }
}
