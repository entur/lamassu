import { TableRow, TableCell, IconButton, Box } from '@mui/material';
import { KeyboardArrowDown, KeyboardArrowUp } from '@mui/icons-material';
import type { StopPlace } from '../../data/StopPlaceContext.tsx';
import DataTableDetail from './DataTableDetail.tsx';
import { useState } from 'react';
import { getIconUrl } from '../../utils/iconLoaderUtils.ts';
import { useTranslation } from 'react-i18next';

interface Props {
  sp: StopPlace;
  useCompactView: boolean;
}

export default function DataTableRow({ sp, useCompactView }: Props) {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const [lng, lat] = sp.geometry.legacyCoordinates?.[0] ?? ['', ''];

  let iconKey: string;
  if (sp.__typename === 'ParentStopPlace') {
    iconKey = 'parentStopPlace';
  } else if (sp.stopPlaceType) {
    iconKey = sp.stopPlaceType;
  } else {
    iconKey = 'default';
  }

  const iconUrl = getIconUrl(iconKey);

  return (
    <>
      <TableRow
        hover
        onClick={useCompactView ? () => setOpen(o => !o) : undefined}
        sx={{ cursor: useCompactView ? 'pointer' : 'inherit' }}
      >
        {useCompactView && (
          <TableCell padding="none">
            <IconButton size="small" onClick={() => setOpen(o => !o)}>
              {open ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
            </IconButton>
          </TableCell>
        )}
        <TableCell>{sp.name.value}</TableCell>
        <TableCell>{sp.id}</TableCell>
        {!useCompactView && <TableCell>{lng || '—'}</TableCell>}
        {!useCompactView && <TableCell>{lat || '—'}</TableCell>}
        {!useCompactView && (
          <TableCell>
            <Box
              component="img"
              src={iconUrl}
              alt={t('data.table.row.typeIconAlt', 'Stop place type icon')}
              sx={{ width: 32 }}
            />
          </TableCell>
        )}
      </TableRow>
      {useCompactView && (
        <DataTableDetail
          open={open}
          lng={lng}
          lat={lat}
          iconUrl={iconUrl}
          stopPlaceType={sp.stopPlaceType}
        />
      )}
    </>
  );
}
