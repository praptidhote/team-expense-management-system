import React from 'react';
import { STATUS_STYLES } from '../../utils/constants';

export const StatusBadge = ({ status }) => {
  const style = STATUS_STYLES[status] || STATUS_STYLES.PENDING;

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold border ${style.bg} ${style.text} ${style.border}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${style.dot}`} />
      {status}
    </span>
  );
};
