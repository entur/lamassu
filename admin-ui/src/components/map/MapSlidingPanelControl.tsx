import { useState, type ReactNode } from 'react';
import { Box, Collapse, IconButton, Paper, Typography, useTheme } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

interface MapSlidingPanelControlProps {
  controlId: string;
  icon: ReactNode;
  panelTitle?: string;
  panelContent: ReactNode;
  ariaLabelOpen: string;
  ariaLabelClose: string;
  initialOpen?: boolean;
  panelMinWidth?: number | string;
  onPanelToggle?: (isOpen: boolean) => void;
}

export function MapSlidingPanelControl({
  controlId,
  icon,
  panelTitle,
  panelContent,
  ariaLabelOpen,
  ariaLabelClose,
  initialOpen = false,
  panelMinWidth = 220,
  onPanelToggle,
}: MapSlidingPanelControlProps) {
  const [isOpen, setIsOpen] = useState(initialOpen);
  const theme = useTheme();

  const fixedPanelWidth =
    typeof panelMinWidth === 'number' ? panelMinWidth : parseInt(panelMinWidth.toString(), 10);
  const gap = parseInt(theme.spacing(1).toString(), 10); // e.g., 8px

  const handleToggle = () => {
    const newOpenState = !isOpen;
    setIsOpen(newOpenState);
    if (onPanelToggle) {
      onPanelToggle(newOpenState);
    }
  };

  const panelId = `${controlId}-panel`;
  const titleId = `${controlId}-title`;

  const buttonTranslateX = isOpen ? `translateX(-${gap}px)` : 'translateX(0px)';

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'flex-start',
        position: 'relative',
      }}
    >
      <IconButton
        onClick={handleToggle}
        aria-label={isOpen ? ariaLabelClose : ariaLabelOpen}
        aria-expanded={isOpen}
        aria-controls={isOpen ? panelId : undefined}
        sx={{
          backgroundColor: theme.palette.background.paper,
          boxShadow: theme.shadows[3],
          transition: theme.transitions.create(['transform', 'box-shadow', 'background-color'], {
            duration: theme.transitions.duration.short,
            easing: theme.transitions.easing.easeInOut,
          }),
          transform: buttonTranslateX,
          zIndex: 2,
          flexShrink: 0,
          '&:hover': {
            transform: `${buttonTranslateX} scale(1.1)`,
            boxShadow: theme.shadows[6],
            backgroundColor: theme.palette.action.hover,
          },
        }}
      >
        {icon}
      </IconButton>
      <Collapse in={isOpen} orientation="horizontal" timeout="auto" unmountOnExit={false}>
        <Paper
          elevation={4}
          sx={{
            p: 2,
            width: fixedPanelWidth,
            minWidth: fixedPanelWidth,
            boxSizing: 'border-box',
            height: '100%',
          }}
          id={panelId}
          role="region"
          aria-labelledby={panelTitle ? titleId : undefined}
        >
          {panelTitle && (
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
              <Typography variant="subtitle1" fontWeight="medium" id={titleId}>
                {panelTitle}
              </Typography>
              <IconButton
                onClick={handleToggle}
                size="small"
                aria-label={ariaLabelClose}
                sx={{ ml: 1 }}
              >
                <CloseIcon fontSize="small" />
              </IconButton>
            </Box>
          )}
          {panelContent}
        </Paper>
      </Collapse>
    </Box>
  );
}
