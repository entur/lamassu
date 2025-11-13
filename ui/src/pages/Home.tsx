import { Container, Typography, Paper, useTheme } from '@mui/material';

export default function HomePage() {
  const theme = useTheme();

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Paper
        elevation={3}
        sx={{
          p: { xs: 2, sm: 2.5, md: 3 },
          textAlign: 'center',
          backgroundColor: theme.palette.background.paper,
        }}
      >
        <Typography
          variant="h3"
          component="h1"
          gutterBottom
          sx={{ fontWeight: 'bold', color: theme.palette.primary.main }}
        >
          Lamassu Admin
        </Typography>
      </Paper>
    </Container>
  );
}
