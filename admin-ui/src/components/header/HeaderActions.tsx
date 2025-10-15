import { Box, IconButton } from '@mui/material';
import { getIconUrl } from '../../utils/iconLoaderUtils.ts';

interface HeaderActionsProps {
  onSettingsIconClick: () => void;
  onMenuIconClick: () => void;
}

export default function HeaderActions({
  onSettingsIconClick,
  onMenuIconClick,
}: HeaderActionsProps) {
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
      <IconButton color="inherit" onClick={onSettingsIconClick} aria-label="settings">
        {renderHeaderIcon('settings')}
      </IconButton>
      <IconButton color="inherit" onClick={onMenuIconClick} aria-label="menu">
        {renderHeaderIcon('menu')}
      </IconButton>
    </Box>
  );
}
