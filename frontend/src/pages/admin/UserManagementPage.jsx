import React, { useState, useEffect, useCallback } from 'react';
import { userService } from '../../services/userService';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Select } from '../../components/common/Select';
import { Modal } from '../../components/common/Modal';
import { Pagination } from '../../components/common/Pagination';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { UserPlus, UserCheck, UserX, Shield, UserCog, AlertCircle } from 'lucide-react';

export const UserManagementPage = () => {
  const [users, setUsers] = useState([]);
  const [managers, setManagers] = useState([]);
  const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);

  // Assign Manager Modal State
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedManagerId, setSelectedManagerId] = useState('');
  const [savingManager, setSavingManager] = useState(false);
  const [modalError, setModalError] = useState('');

  const loadUsers = useCallback(async (page = 0) => {
    setLoading(true);
    try {
      const [usersData, managersList] = await Promise.all([
        userService.getAllUsers({ page, size: 10 }),
        userService.getActiveManagers(),
      ]);
      setUsers(usersData.content);
      setPageInfo({
        page: usersData.page,
        size: usersData.size,
        totalPages: usersData.totalPages,
        totalElements: usersData.totalElements,
      });
      setManagers(managersList.map((m) => ({ value: m.id, label: `${m.fullName} (${m.email})` })));
    } catch (err) {
      console.error('Failed to load user management', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadUsers(0);
  }, [loadUsers]);

  const handleToggleStatus = async (user) => {
    const newStatus = !user.active;
    const confirmMsg = newStatus
      ? `Reactivate account for ${user.fullName}?`
      : `Deactivate account for ${user.fullName}? They will not be able to log in.`;
    if (!window.confirm(confirmMsg)) return;

    try {
      await userService.updateUserStatus(user.id, newStatus);
      loadUsers(pageInfo.page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update user status');
    }
  };

  const handleOpenAssignManager = (user) => {
    setSelectedUser(user);
    setSelectedManagerId(user.managerId ? String(user.managerId) : '');
    setModalError('');
  };

  const handleSaveManager = async (e) => {
    e.preventDefault();
    setSavingManager(true);
    setModalError('');
    try {
      const managerId = selectedManagerId ? Number(selectedManagerId) : null;
      await userService.assignManager(selectedUser.id, managerId);
      setSelectedUser(null);
      loadUsers(pageInfo.page);
    } catch (err) {
      setModalError(err.response?.data?.message || 'Failed to assign manager');
    } finally {
      setSavingManager(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 tracking-tight">User Management</h1>
        <p className="text-sm text-slate-500 mt-1">
          Manage workforce accounts, assign reporting managers, and control login permissions
        </p>
      </div>

      <Card>
        {loading ? (
          <LoadingSpinner text="Loading user directory..." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm text-slate-600">
              <thead className="bg-slate-50 text-[11px] uppercase font-semibold text-slate-500 border-b border-slate-100">
                <tr>
                  <th className="px-4 py-3">Employee</th>
                  <th className="px-4 py-3">Department & Title</th>
                  <th className="px-4 py-3">Role</th>
                  <th className="px-4 py-3">Assigned Manager</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="px-4 py-4">
                      <p className="font-semibold text-slate-900">{u.fullName}</p>
                      <p className="text-xs text-slate-400">{u.email}</p>
                    </td>
                    <td className="px-4 py-4">
                      <p className="font-medium text-slate-800">{u.jobTitle || 'N/A'}</p>
                      <p className="text-xs text-slate-400">{u.department || 'General'}</p>
                    </td>
                    <td className="px-4 py-4">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-slate-100 text-slate-700">
                        {u.roles?.join(', ').replace(/ROLE_/g, '')}
                      </span>
                    </td>
                    <td className="px-4 py-4 text-xs font-medium text-slate-700">
                      {u.managerName ? (
                        <span>{u.managerName}</span>
                      ) : (
                        <span className="text-slate-400 italic">None assigned</span>
                      )}
                    </td>
                    <td className="px-4 py-4">
                      <span
                        className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                          u.active
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : 'bg-rose-50 text-rose-700 border border-rose-200'
                        }`}
                      >
                        <span className={`w-1.5 h-1.5 rounded-full ${u.active ? 'bg-emerald-500' : 'bg-rose-500'}`} />
                        {u.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-4 py-4 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Button
                          variant="secondary"
                          size="sm"
                          icon={UserCog}
                          onClick={() => handleOpenAssignManager(u)}
                        >
                          Manager
                        </Button>
                        <Button
                          variant={u.active ? 'secondary' : 'primary'}
                          size="sm"
                          className={u.active ? 'text-rose-600 hover:bg-rose-50' : ''}
                          onClick={() => handleToggleStatus(u)}
                        >
                          {u.active ? 'Deactivate' : 'Activate'}
                        </Button>
                      </div>
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
          onPageChange={(p) => loadUsers(p)}
        />
      </Card>

      {/* Assign Manager Modal */}
      <Modal
        isOpen={!!selectedUser}
        onClose={() => setSelectedUser(null)}
        title={`Assign Manager to ${selectedUser?.fullName}`}
      >
        <form onSubmit={handleSaveManager} className="space-y-4">
          {modalError && (
            <div className="flex items-center gap-2 p-3 rounded-xl bg-rose-50 text-rose-700 text-xs border border-rose-200">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{modalError}</span>
            </div>
          )}

          <p className="text-xs text-slate-500">
            Expenses submitted by <strong>{selectedUser?.fullName}</strong> will be routed to this manager's approval queue.
          </p>

          <Select
            label="Designate Reporting Manager"
            name="managerId"
            options={managers.filter((m) => m.value !== selectedUser?.id)}
            placeholder="Select a manager (or unassign)"
            value={selectedManagerId}
            onChange={(e) => setSelectedManagerId(e.target.value)}
          />

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setSelectedUser(null)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" loading={savingManager}>
              Save Assignment
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
