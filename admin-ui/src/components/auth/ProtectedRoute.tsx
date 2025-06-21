import React from 'react';
import { useAuth } from '../../auth';
import LoginRedirect from '../../auth/LoginRedirect';

interface ProtectedRouteProps {
  element: React.ReactElement;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ element }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div>Loading authentication status...</div>;
  }

  if (!isAuthenticated) {
    return <LoginRedirect />;
  }

  return element;
};
