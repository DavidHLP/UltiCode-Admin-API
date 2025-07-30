export const getStatusTagType = (status: string) => {
  switch (status) {
    case 'Accepted':
      return 'success'
    case 'Runtime Error':
    case 'Compile Error':
      return 'danger'
    case 'Wrong Answer':
    case 'Time Limit Exceeded':
    case 'Memory Limit Exceeded':
      return 'warning'
    default:
      return 'warning'
  }
}
