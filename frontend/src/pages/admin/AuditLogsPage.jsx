import React, { useState, useEffect, useCallback } from 'react';
import { auditLogService } from '../../services/auditLogService';
import { Card } from '../../components/common/Card';
import { Pagination } from '../../components/common/Pagination';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { formatDateTime } from '../../utils/formatters';
import { ShieldAlert, User, Activity, Clock } from 'lucide-react';

export const AuditLogsPage = () => {
  const [logs, setLogs] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 20, totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);

  const fetchLogs = useCallback(async (page = 0) => {
    setLoading(true);
    try {
      const data = await auditLogService.getAuditLogs({ page, size: 20 });
      setLogs(data.content);
      setPageInfo({
        page: data.page,
        size: data.size,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
      });
    } catch (err) {
      console.error('Failed to load audit logs', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchLogs(0);
  }, [fetchLogs]);

  const getActionBadge = (action) => {
    let color = 'bg-slate-100 text-slate-700';
    if (action.includes('APPROVE')) color = 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    else if (action.includes('REJECT') || action.includes('CANCEL')) color = 'bg-rose-50 text-rose-700 border border-rose-200';
    else if (action.includes('CREATE')) color = 'bg-blue-50 text-blue-700 border border-blue-200';
    else if (action.includes('LOGIN')) color = 'bg-purple-50 text-purple-700 border border-purple-200';

    return (
      <span className={`inline-flex px-2 py-0.5 rounded text-[11px] font-bold ${color}`}>
        {action}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">System Audit Trail</h1>
        <p className="text-sm text-slate-500 mt-1">
          Immutable audit record of all financial operations, approval decisions, and user account actions
        </p>
      </div>

      <Card>
        {loading ? (
          <LoadingSpinner text="Querying audit records..." />
        ) : logs.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">No audit logs recorded yet</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-[11px] uppercase font-semibold text-slate-500 border-b border-slate-100">
                <tr>
                  <th className="px-4 py-3">Timestamp</th>
                  <th className="px-4 py-3">Actor</th>
                  <th className="px-4 py-3">Action</th>
                  <th className="px-4 py-3">Entity</th>
                  <th className="px-4 py-3">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-4 py-3 text-slate-400 whitespace-nowrap">
                      {formatDateTime(log.createdAt)}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <p className="font-semibold text-slate-800">{log.actorName || 'System'}</p>
                      <p className="text-[10px] text-slate-400">{log.actorEmail}</p>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">{getActionBadge(log.action)}</td>
                    <td className="px-4 py-3 font-medium text-slate-700 whitespace-nowrap">
                      {log.entityName} #{log.entityId}
                    </td>
                    <td className="px-4 py-3 text-slate-600 max-w-md truncate" title={log.details}>
                      {log.details || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          page={pageInfo.page}
          size={pageInfo.size}
          totalPages={pageInfo.totalPages}
          totalElements={pageInfo.totalElements}
          onPageChange={(p) => fetchLogs(p)}
        />
      </Card>
    </div>
  );
};
