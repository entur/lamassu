import { useEffect, useState } from 'react';
import type { StopPlace, StopPlaceContext } from './StopPlaceContext';
import { fetchStopPlaces } from './fetchStopPlaces';

export type Order = 'asc' | 'desc';
export type OrderBy = 'name' | 'id';

export function useStopPlaces() {
  const [data, setData] = useState<StopPlace[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [order, setOrder] = useState<Order>('asc');
  const [orderBy, setOrderBy] = useState<OrderBy>('name');

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  useEffect(() => {
    fetchStopPlaces()
      .then((ctx: StopPlaceContext) => {
        setData(ctx.data.stopPlace);
      })
      .catch(() => setError('Failed to fetch data'))
      .finally(() => setLoading(false));
  }, []);

  const handleRequestSort = (property: OrderBy) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const sorted = [...data].sort((a, b) => {
    const v1 = orderBy === 'name' ? a.name.value.toLowerCase() : a.id.toLowerCase();
    const v2 = orderBy === 'name' ? b.name.value.toLowerCase() : b.id.toLowerCase();
    if (v1 < v2) return order === 'asc' ? -1 : 1;
    if (v1 > v2) return order === 'asc' ? 1 : -1;
    return 0;
  });

  const paginated = sorted.slice(page * rowsPerPage, (page + 1) * rowsPerPage);

  return {
    allData: sorted,
    data: paginated,
    totalCount: data.length,
    loading,
    error,
    order,
    orderBy,
    handleRequestSort,
    page,
    rowsPerPage,
    setPage,
    setRowsPerPage,
  };
}
