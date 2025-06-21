import { useCallback } from 'react';
import { useAuth as useOidcAuth } from 'react-oidc-context';
import { useConfig } from '../contexts/ConfigContext.tsx';

export interface Auth {
  isLoading: boolean;
  isAuthenticated: boolean;
  user?: {
    name?: string;
  };
  roleAssignments?: string[] | null;
  getAccessToken: () => Promise<string>;
  logout: ({ returnTo }: { returnTo?: string }) => Promise<void>;
  login: (redirectUri?: string) => Promise<void>;
}

export const useAuth = (): Auth => {
  const { isLoading, isAuthenticated, user, signoutRedirect, signinRedirect } = useOidcAuth();

  const { claimsNamespace, preferredNameNamespace } = useConfig();

  const getAccessToken = useCallback(() => {
    return new Promise<string>((resolve, reject) => {
      const accessToken = user?.access_token;
      if (accessToken) {
        resolve(accessToken);
      } else {
        reject();
      }
    });
  }, [user]);

  const logout = useCallback(
    ({ returnTo }: { returnTo?: string }) => {
      return signoutRedirect({ post_logout_redirect_uri: returnTo });
    },
    [signoutRedirect]
  );

  const login = useCallback(
    (redirectUri?: string) => signinRedirect({ redirect_uri: redirectUri }),
    [signinRedirect]
  );

  return {
    isLoading,
    isAuthenticated,
    user: {
      name: user?.profile[preferredNameNamespace!] as string,
    },
    roleAssignments: user?.profile[claimsNamespace!] as string[],
    getAccessToken,
    logout,
    login,
  };
};
