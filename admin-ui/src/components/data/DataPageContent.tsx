import { useRef } from 'react';
import { useStopPlaces } from '../../data/useStopPlaces';

import { useContainerResponsiveView } from '../../hooks/useContainerResponsiveView';
import { Box, Table, TableBody, TableContainer, TablePagination, Typography } from '@mui/material';
import DataTableHeader from './DataTableHeader.tsx';
import DataTableRow from './DataTableRow.tsx';
import { useTranslation } from 'react-i18next';

const COMPACT_VIEW_THRESHOLD = 700;

export default function DataPageContent() {
  const { t } = useTranslation();
  const {
    data,
    totalCount,
    loading,
    order,
    orderBy,
    handleRequestSort,
    page,
    rowsPerPage,
    setPage,
    setRowsPerPage,
  } = useStopPlaces();

  const containerRef = useRef<HTMLDivElement>(null);
  const compact = useContainerResponsiveView(containerRef, COMPACT_VIEW_THRESHOLD, loading);

  return (
    <Box
      ref={containerRef}
      sx={{
        flexGrow: 1,
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      <Box p={2}>
        <Typography variant="h4" component="h2" align="center">
          {t('data.title', 'Stop Place Overview')}
        </Typography>
      </Box>
      <Box px={2} pb={1}>
        <Typography>{t('data.totalEntries', { count: totalCount })}</Typography>
      </Box>

      <TableContainer sx={{ flexGrow: 1, overflow: 'auto' }}>
        <Table stickyHeader>
          <DataTableHeader
            useCompactView={compact}
            order={order}
            orderBy={orderBy}
            onRequestSort={handleRequestSort}
          />
          <TableBody>
            {data.map(sp => (
              <DataTableRow key={`${sp.id}-${sp.version}`} sp={sp} useCompactView={compact} />
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        sx={{
          p: 0,
          m: 0,
          flexShrink: 0,
        }}
        component="div"
        count={totalCount}
        page={page}
        rowsPerPage={rowsPerPage}
        onPageChange={(_, p) => setPage(p)}
        onRowsPerPageChange={e => {
          setRowsPerPage(+e.target.value);
          setPage(0);
        }}
        rowsPerPageOptions={[10, 25, 50, 100]}
        labelRowsPerPage={t('data.pagination.rowsPerPage', 'Rows per page:')}
        labelDisplayedRows={({ from, to, count }) => {
          const key =
            count === -1 ? 'data.pagination.displayedRowsOfMore' : 'data.pagination.displayedRows';
          return t(key, { from, to, count });
        }}
      />
    </Box>
  );
}
