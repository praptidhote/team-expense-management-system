import React from 'react';

export const LoadingSpinner = ({ size = 'md', text = 'Loading...' }) => {
  const sizes = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  };

  return (
    <div className="flex flex-col items-center justify-center p-8 text-slate-500">
      <div
        className={`${sizes[size]} animate-spin rounded-full border-2 border-slate-200 border-t-brand-600 mb-3`}
      />
      {text && <span className="text-xs font-medium text-slate-500">{text}</span>}
    </div>
  );
};
