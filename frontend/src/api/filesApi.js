import { httpClient } from './httpClient'

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return httpClient.post('/api/v1/files', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadFile(fileId) {
  return httpClient.get(`/api/v1/files/${fileId}`, {
    responseType: 'blob'
  })
}

export function deleteFile(fileId) {
  return httpClient.delete(`/api/v1/files/${fileId}`)
}
