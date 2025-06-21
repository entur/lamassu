import { Box, TextField, InputAdornment, IconButton, useTheme } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import CloseIcon from '@mui/icons-material/Close';

interface MobileSearchBarProps {
  searchQuery: string;
  onSearchQueryChange: (value: string) => void;
  onClose: () => void;
  placeholder: string;
}

export default function MobileSearchBar({
  searchQuery,
  onSearchQueryChange,
  onClose,
  placeholder,
}: MobileSearchBarProps) {
  const theme = useTheme();
  return (
    <Box sx={{ flexGrow: 1, width: '100%' }}>
      <TextField
        autoFocus
        size="small"
        placeholder={placeholder}
        variant="outlined"
        fullWidth
        value={searchQuery}
        onChange={e => onSearchQueryChange(e.target.value)}
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
            endAdornment: (
              <InputAdornment position="end">
                <IconButton size="small" onClick={onClose}>
                  <CloseIcon />
                </IconButton>
              </InputAdornment>
            ),
          },
        }}
      />
    </Box>
  );
}
