import React, { useState } from 'react';
import {
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tooltip,
  IconButton,
} from '@mui/material';
import { InfoOutlined } from '@mui/icons-material';
import Typography from '@mui/material/Typography';
import { useTranslation } from 'react-i18next';

export interface WorkAreaContentProps {
  onSave?: (data: { name: string; type: string; include: boolean }) => void;
  onCancel?: () => void;
  onDetailsOpen?: () => void;
}

const WorkAreaContent: React.FC<WorkAreaContentProps> = () => {
  const { t } = useTranslation();
  const [name, setName] = useState<string>('');
  const [type, setType] = useState<string>('train');
  const [include, setInclude] = useState<boolean>(false);
  const [dialogOpen, setDialogOpen] = useState<boolean>(false);

  const handleSave = () => {};

  const handleCancel = () => {
    setName('');
    setType('train');
    setInclude(false);
  };

  const handleDetailsOpen = () => {
    setDialogOpen(true);
  };

  const handleDetailsClose = () => {
    setDialogOpen(false);
  };

  const typeOptions = [
    { value: 'train', labelKey: 'workArea.types.train' },
    { value: 'bus', labelKey: 'workArea.types.bus' },
    { value: 'ferry', labelKey: 'workArea.types.ferry' },
    { value: 'tram', labelKey: 'workArea.types.tram' },
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, p: 2 }}>
      <Typography variant="h5" component="h1">
        {t('workArea.title', 'Work Area')}
      </Typography>

      <TextField
        label={t('workArea.nameLabel', 'Name')}
        value={name}
        onChange={e => setName(e.target.value)}
        fullWidth
      />

      <FormControl fullWidth>
        <InputLabel id="type-label">{t('workArea.typeLabel', 'Type')}</InputLabel>
        <Select
          labelId="type-label"
          label={t('workArea.typeLabel', 'Type')}
          value={type}
          onChange={e => setType(e.target.value)}
        >
          {typeOptions.map(option => (
            <MenuItem key={option.value} value={option.value}>
              {t(option.labelKey, option.value.charAt(0).toUpperCase() + option.value.slice(1))}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <FormControlLabel
        control={<Checkbox checked={include} onChange={e => setInclude(e.target.checked)} />}
        label={t('workArea.includeLabel', 'Include')}
      />

      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        <Button variant="contained" onClick={handleSave}>
          {t('save')}
        </Button>
        <Button variant="outlined" onClick={handleCancel}>
          {t('cancel')}
        </Button>
        <Button variant="text" onClick={handleDetailsOpen}>
          {t('details')}
        </Button>
        <Tooltip title={t('workArea.moreInfoTooltip', 'More info')}>
          <IconButton>
            <InfoOutlined />
          </IconButton>
        </Tooltip>
      </Box>

      <Dialog open={dialogOpen} onClose={handleDetailsClose} fullWidth>
        <DialogTitle>{t('details')}</DialogTitle>
        <DialogContent></DialogContent>
        <DialogActions>
          <Button onClick={handleDetailsClose}>{t('close', 'Close')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default WorkAreaContent;
