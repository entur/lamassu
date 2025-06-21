import { useEffect } from 'react';
import { useAuth } from './index';
import { useLocation, useNavigate } from 'react-router-dom';
import { useConfig } from '../contexts/ConfigContext.tsx';

const LoginRedirect = () => {
  const { oidcConfig } = useConfig();
  const { isAuthenticated, isLoading, login } = useAuth();
  const navigate = useNavigate();
  const returnUrl = useLocation().pathname;

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      login(oidcConfig?.redirect_uri || window.location.origin + returnUrl);
    } else if (isAuthenticated) {
      navigate(returnUrl);
    }
  }, [isLoading, isAuthenticated, login, navigate, returnUrl, oidcConfig?.redirect_uri]);

  if (isLoading) return <div>Checking authentication status...</div>;

  return <div>Redirecting to login provider...</div>;
};

export default LoginRedirect;
