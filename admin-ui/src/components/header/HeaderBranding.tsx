import { Link } from 'react-router-dom';
import { Box, Typography, useTheme } from '@mui/material';

export default function HeaderBranding() {
  const theme = useTheme();

  return (
    <Box sx={{ display: 'flex', alignItems: 'center' }}>
      <Link
        to="/"
        style={{
          display: 'flex',
          alignItems: 'center',
          textDecoration: 'none',
          color: 'inherit',
        }}
      >
        <img src={theme.logoUrl} alt="logo" height={theme.logoHeight} />
        <Typography variant="h6" sx={{ ml: 1, mr: 1, fontWeight: 'bold' }}>
          {theme.applicationName}
        </Typography>
      </Link>
    </Box>
  );
}
