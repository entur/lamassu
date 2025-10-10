import { Box, IconButton, Avatar, Typography, useTheme } from '@mui/material';
import { getIconUrl } from '../../utils/iconLoaderUtils.ts';

interface HeaderActionsProps {
  onUserIconClick: () => void;
  onSettingsIconClick: () => void;
  onMenuIconClick: () => void;
  isAuthenticated: boolean;
  userInitials: string;
}

export default function HeaderActions({
  onUserIconClick,
  onSettingsIconClick,
  onMenuIconClick,
  isAuthenticated,
  userInitials,
}: HeaderActionsProps) {
  const theme = useTheme();

  const renderHeaderIcon = (key: string, size = 28) => (
    <Box
      component="img"
      src={getIconUrl(key)}
      alt={`${key} icon`}
      sx={{ width: size, height: size }}
    />
  );

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', ml: 'auto' }}>
      <IconButton color="inherit" onClick={onUserIconClick} aria-label="user account">
        {isAuthenticated && userInitials ? (
          <Avatar
            className="avatar"
            sx={{
              bgcolor: theme.palette.common.white,
              color: theme.palette.secondary.main,
              fontWeight: 'bold',
              width: 28,
              height: 28,
            }}
          >
            <Typography className="initials" sx={{ fontSize: '0.8rem' }}>
              {' '}
              {userInitials}
            </Typography>
          </Avatar>
        ) : (
          renderHeaderIcon('user')
        )}
      </IconButton>

      <IconButton color="inherit" onClick={onSettingsIconClick} aria-label="settings">
        {renderHeaderIcon('settings')}
      </IconButton>
      <IconButton color="inherit" onClick={onMenuIconClick} aria-label="menu">
        {renderHeaderIcon('menu')}
      </IconButton>
    </Box>
  );
}
