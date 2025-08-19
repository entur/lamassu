import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { useAuth } from '../../auth';
import { useTranslation } from 'react-i18next';

interface UserDialogProps {
  open: boolean;
  onClose: () => void;
  onLogout: () => void;
}

export default function UserDialog({ open, onClose, onLogout }: UserDialogProps) {
  const { t } = useTranslation();
  const auth = useAuth();

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{t('user.userAccount')}</DialogTitle>
      <DialogContent dividers>
        <Typography variant="caption">{t('user.name')}: </Typography>
        <Typography variant="body1">{auth.user?.name}</Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onLogout} variant="contained">
          {t('user.logout')}
        </Button>
        <Button onClick={onClose} variant="outlined">
          {t('close')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
