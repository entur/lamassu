import { Box, TextField, InputAdornment, useTheme } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

interface DesktopSearchBarProps {
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
  placeholder: string;
}

export default function DesktopSearchBar({
  searchQuery,
  onSearchQueryChange,
  placeholder,
}: DesktopSearchBarProps) {
  const theme = useTheme();
  return (
    <Box
      sx={{
        flexGrow: 1,
        display: 'flex',
        justifyContent: 'center',
      }}
    >
      <TextField
        size="small"
        placeholder={placeholder}
        variant="outlined"
        value={searchQuery}
        onChange={e => onSearchQueryChange(e.target.value)}
        fullWidth
        slotProps={{
          input: {
            sx: {
              backgroundColor: theme.palette.background.default,
            },
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          },
        }}
        sx={{
          width: '100%',
          maxWidth: 400,
        }}
      />
    </Box>
  );
}
