import { Link } from 'react-router-dom';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Divider,
  Box,
  Typography,
  useTheme,
  useMediaQuery,
  IconButton,
  styled,
} from '@mui/material';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { getIconUrl } from '../utils/iconLoaderUtils.ts';
import { useTranslation } from 'react-i18next';

const DESKTOP_WIDTH = 280;

const StyledDrawer = styled(Drawer)(({ theme }) => ({
  '& .MuiDrawer-paper': {
    boxSizing: 'border-box',
    backgroundColor: theme.palette.background.paper,
    transition: theme.transitions.create(['width'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
}));

const menuItems = [{ textKey: 'home', path: '/', iconKey: 'home' }];

const adminMenuItems = [
  { textKey: 'admin.feedProviders', path: '/feed-providers', iconKey: 'settings' },
  { textKey: 'admin.cacheManagement', path: '/cache', iconKey: 'settings' },
  { textKey: 'admin.spatialIndex', path: '/spatial-index', iconKey: 'settings' },
];

interface SideMenuProps {
  open: boolean;
  onClose: () => void;
}

export default function Menu({ open, onClose }: SideMenuProps) {
  const { t } = useTranslation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  return (
    <StyledDrawer
      variant={isMobile ? 'temporary' : 'persistent'}
      anchor="right"
      open={open}
      onClose={onClose}
      ModalProps={{ keepMounted: true }}
      slotProps={{
        paper: {
          sx: {
            width: isMobile ? '100%' : DESKTOP_WIDTH,
            borderLeft: isMobile ? 'none' : `1px solid ${theme.palette.divider}`,
          },
        },
      }}
    >
      <Toolbar
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          px: 1,
          backgroundColor: theme.palette.primary.main,
          color: theme.palette.common.white,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <img src={theme.logoUrl} alt="logo" height={24} />
          <Typography variant="h6" noWrap sx={{ ml: 1 }}>
            {theme.applicationName}
          </Typography>
        </Box>
        <IconButton onClick={onClose} color="inherit">
          <ChevronRightIcon />
        </IconButton>
      </Toolbar>
      <Divider />

      <List disablePadding>
        {menuItems.map(({ textKey, path, iconKey }) => (
          <ListItem key={path} disablePadding>
            <ListItemButton component={Link} to={path} onClick={onClose}>
              <ListItemIcon>
                <Box
                  component="img"
                  src={getIconUrl(iconKey)}
                  alt={t(textKey)}
                  sx={{ width: 24, height: 24 }}
                />
              </ListItemIcon>
              <ListItemText primary={t(textKey)} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>

      <Divider />

      <List disablePadding>
        {adminMenuItems.map(({ textKey, path, iconKey }) => (
          <ListItem key={path} disablePadding>
            <ListItemButton component={Link} to={path} onClick={onClose}>
              <ListItemIcon>
                <Box
                  component="img"
                  src={getIconUrl(iconKey)}
                  alt={t(textKey)}
                  sx={{ width: 24, height: 24 }}
                />
              </ListItemIcon>
              <ListItemText primary={t(textKey)} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>

      <Box sx={{ flexGrow: 1 }} />
      <Divider />
      <Box p={2} textAlign="center">
        <Typography variant="caption" color={theme.palette.secondary.main}>
          {theme.companyName}
        </Typography>
      </Box>
    </StyledDrawer>
  );
}
