import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../components/common/Button';
import { Home } from 'lucide-react';

export const NotFoundPage = () => {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-6 text-center">
      <div className="h-20 w-20 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600 font-extrabold text-3xl mb-4">
        404
      </div>
      <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Page Not Found</h1>
      <p className="text-sm text-slate-500 max-w-sm mt-1 mb-6">
        The requested screen does not exist, or you lack the required managerial credentials to view it.
      </p>
      <Link to="/dashboard">
        <Button variant="primary" icon={Home}>
          Return to Dashboard
        </Button>
      </Link>
    </div>
  );
};
