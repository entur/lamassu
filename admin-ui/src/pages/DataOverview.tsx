import { useRef } from 'react';
import { Box, useTheme } from '@mui/material';
import { useStopPlaces } from '../data/useStopPlaces';

import { Sidebar } from '../components/sidebar/Sidebar.tsx';
import { ToggleButton } from '../components/sidebar/ToggleButton.tsx';
import { useResizableSidebar } from '../hooks/useResizableSidebar.ts';
import DataPageContent from '../components/data/DataPageContent.tsx';
import LoadingPage from '../components/common/LoadingPage.tsx';
import ErrorPage from '../components/common/ErrorPage.tsx';

export default function DataOverview() {
  const theme = useTheme();

  const { loading, error } = useStopPlaces();

  const {
    width: sidebarWidth,
    collapsed: sidebarCollapsed,
    setIsResizing: setIsSidebarResizing,
    toggle: toggleSidebar,
  } = useResizableSidebar(250);

  const tableContentContainerRef = useRef<HTMLDivElement | null>(null);

  if (loading) return <LoadingPage />;
  if (error) return <ErrorPage />;

  return (
    <Box
      sx={{
        display: 'flex',
        height: 'calc(100vh - 64px)',
        position: 'relative',
      }}
    >
      <Sidebar
        width={sidebarWidth}
        collapsed={sidebarCollapsed}
        onMouseDownResize={() => setIsSidebarResizing(true)}
        theme={theme}
        toggleCollapse={toggleSidebar}
      />
      <ToggleButton
        collapsed={sidebarCollapsed}
        sidebarWidth={sidebarWidth}
        onClick={toggleSidebar}
      />
      <Box
        ref={tableContentContainerRef}
        className="data-overview-content"
        sx={{
          flexGrow: 1,
          height: '100%',
          marginLeft: sidebarCollapsed ? '0px' : `${sidebarWidth + 4}px`,
          transition: 'margin-left 0.2s ease',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <DataPageContent></DataPageContent>
      </Box>
    </Box>
  );
}
