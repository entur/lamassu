import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Switch,
  Box,
  FormControl,
  Select,
  MenuItem,
  Stack,
  type SelectChangeEvent,
} from '@mui/material';
import { useCustomization } from '../../contexts/CustomizationContext.tsx';
import { useTranslation } from 'react-i18next';

interface SettingsDialogProps {
  open: boolean;
  onClose: () => void;
}

export default function SettingsDialog({ open, onClose }: SettingsDialogProps) {
  const { useCustomFeatures, toggleCustomFeatures } = useCustomization();
  const { i18n, t } = useTranslation();

  const handleLanguageChange = (event: SelectChangeEvent) => {
    i18n.changeLanguage(event.target.value as string);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{t('settings')}</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={3}>
          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Typography>{t('settings.enableCustomTheme')}</Typography>
            <Switch
              checked={useCustomFeatures}
              onChange={toggleCustomFeatures}
              name="customFeaturesSwitch"
            />
          </Box>

          <Box display="flex" alignItems="center" justifyContent="space-between">
            <Typography>{t('settings.language')}</Typography>
            <FormControl>
              <Select
                labelId="language-select-label"
                value={i18n.resolvedLanguage}
                onChange={handleLanguageChange}
                label={t('settings.language')}
                notched={false}
              >
                <MenuItem value="en">English</MenuItem>
                <MenuItem value="nb">Norsk (Bokmål)</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button variant="outlined" onClick={onClose}>
          {t('close')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
