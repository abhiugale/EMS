import { useMutation } from '@tanstack/react-query';
import { registerUser } from '../service/adminService';

export const useRegisterUser = () => useMutation({ mutationFn: registerUser });
