import { TableRow, TableCell, Collapse, Box, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';

interface Props {
  open: boolean;
  lng: number | string;
  lat: number | string;
  iconUrl: string;
  stopPlaceType: string;
}

export default function DataTableDetail({ open, lng, lat, iconUrl }: Props) {
  const { t } = useTranslation();

  return (
    <TableRow>
      <TableCell colSpan={5} sx={{ p: 0 }}>
        {' '}
        <Collapse in={open} timeout="auto" unmountOnExit>
          <Box m={1}>
            <Typography variant="subtitle2">
              {t('data.table.detail.coordinatesTitle', 'Coordinates')}
            </Typography>
            <Typography variant="body2">
              <strong>{t('data.table.detail.longitudeLabel', 'Longitude:')}</strong> {lng || '—'}
            </Typography>
            <Typography variant="body2">
              <strong>{t('data.table.detail.latitudeLabel', 'Latitude:')}</strong> {lat || '—'}
            </Typography>
          </Box>
          <Box m={1}>
            <Typography variant="subtitle2">{t('data.table.detail.typeTitle', 'Type')}</Typography>
            <Box
              component="img"
              src={iconUrl}
              alt={t('data.table.row.typeIconAlt', 'Stop place type icon')}
              sx={{ height: 32 }}
            />
          </Box>
        </Collapse>
      </TableCell>
    </TableRow>
  );
}
