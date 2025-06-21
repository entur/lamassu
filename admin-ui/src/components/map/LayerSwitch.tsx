import { FormControlLabel, Switch, Typography } from '@mui/material';
import type { FC } from 'react';

interface LayerSwitchProps {
  id: string;
  label: string;
  checked: boolean;
  onChange: (id: string, checked: boolean) => void;
}

export const LayerSwitch: FC<LayerSwitchProps> = ({ id, label, checked, onChange }) => (
  <FormControlLabel
    control={
      <Switch size="small" checked={checked} onChange={e => onChange(id, e.target.checked)} />
    }
    label={<Typography variant="body2">{label}</Typography>}
  />
);
