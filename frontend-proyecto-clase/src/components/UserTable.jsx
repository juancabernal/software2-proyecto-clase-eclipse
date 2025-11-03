import React, { useState } from "react";
import { motion } from "framer-motion";
import { ArrowUpDown, ChevronLeft, ChevronRight } from "lucide-react";
import { requestEmailConfirmation, requestMobileConfirmation } from "../services/api";

const UserTable = ({ users, currentPage, totalPages, totalCount = 0, usersPerPage = 10, onPageChange }) => {
  const [sortConfig, setSortConfig] = useState({ key: null, direction: "asc" });
  const [loadingActions, setLoadingActions] = useState({});
  const [feedbackMessages, setFeedbackMessages] = useState({});

  const handleSort = (key) => {
    let direction = "asc";
    if (sortConfig.key === key && sortConfig.direction === "asc") {
      direction = "desc";
    }
    setSortConfig({ key, direction });
  };

  const sortedUsers = [...users].sort((a, b) => {
    if (!sortConfig.key) return 0;
    const valueA = a[sortConfig.key].toString().toLowerCase();
    const valueB = b[sortConfig.key].toString().toLowerCase();

    if (valueA < valueB) return sortConfig.direction === "asc" ? -1 : 1;
    if (valueA > valueB) return sortConfig.direction === "asc" ? 1 : -1;
    return 0;
  });

  const updateFeedback = (userId, variant, message) => {
    setFeedbackMessages((prev) => {
      if (!message) {
        const { [userId]: _removed, ...rest } = prev;
        return rest;
      }

      return {
        ...prev,
        [userId]: { variant, message },
      };
    });
  };

  const handleValidationClick = async (user, type) => {
    const userId = user?.id;
    if (!userId) {
      return;
    }

    const key = `${userId}-${type}`;
    const successMessage =
      type === "email"
        ? "Se envió la solicitud de validación del correo electrónico."
        : "Se envió la solicitud de validación del teléfono móvil.";
    const errorFallback =
      type === "email"
        ? "No fue posible solicitar la validación del correo electrónico."
        : "No fue posible solicitar la validación del teléfono móvil.";

    if (type === "email" && !user?.email) {
      updateFeedback(userId, "error", "El usuario no tiene un correo electrónico registrado.");
      return;
    }

    const hasMobileContact = Boolean(user?.mobileNumber || user?.telefonoMovil || user?.telefono);
    if (type !== "email" && !hasMobileContact) {
      updateFeedback(userId, "error", "El usuario no tiene un teléfono móvil registrado.");
      return;
    }

    setLoadingActions((prev) => ({ ...prev, [key]: true }));
    updateFeedback(userId, null, null);

    try {
      if (type === "email") {
        await requestEmailConfirmation(userId);
      } else {
        await requestMobileConfirmation(userId);
      }
      updateFeedback(userId, "success", successMessage);
    } catch (error) {
      const message = error?.message || errorFallback;
      updateFeedback(userId, "error", message);
      console.error(`Error solicitando validación de ${type}:`, error);
    } finally {
      setLoadingActions((prev) => {
        const { [key]: _removed, ...rest } = prev;
        return rest;
      });
    }
  };

  const renderSortIcon = (key) => (
    <ArrowUpDown
      className={`w-4 h-4 ml-2 inline-block ${
        sortConfig.key === key ? "text-indigo-400" : "text-gray-500"
      }`}
    />
  );

  return (
    <motion.div
      initial={{ opacity: 0, y: 15 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4 }}
      className="bg-[#1E1E1E] rounded-2xl shadow-lg border border-gray-700 overflow-hidden"
    >
      <table className="w-full text-left text-gray-200">
        <thead className="bg-[#2A2A2A] text-gray-300">
          <tr>
            <th
              onClick={() => handleSort("nombre")}
              className="px-6 py-4 cursor-pointer select-none hover:text-white transition-colors"
              style={{ width: '40%' }}
            >
              Nombre {renderSortIcon("nombre")}
            </th>
            <th
              onClick={() => handleSort("email")}
              className="px-6 py-4 cursor-pointer select-none hover:text-white transition-colors break-words"
              style={{ width: '35%' }}
            >
              Correo {renderSortIcon("email")}
            </th>
            <th
              onClick={() => handleSort("rol")}
              className="px-6 py-4 cursor-pointer select-none hover:text-white transition-colors"
              style={{ width: '15%' }}
            >
              Rol {renderSortIcon("rol")}
            </th>
            <th
              className="px-6 py-4 text-gray-300"
              style={{ width: '20%' }}
            >
              Acciones
            </th>
          </tr>
        </thead>
        <tbody>
          {sortedUsers.length === 0 ? (
            <tr>
              <td
                colSpan="4"
                className="text-center py-6 text-gray-500 italic bg-[#181818]"
              >
                No se encontraron usuarios.
              </td>
            </tr>
          ) : (
            sortedUsers.map((user, index) => (
              <motion.tr
                key={user.id}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: index * 0.05 }}
                className="hover:bg-[#252525] transition-colors border-t border-gray-800"
              >
                <td className="px-6 py-4">{user.nombre}</td>
                <td className="px-6 py-4">{user.email}</td>
                <td className="px-6 py-4 capitalize">
                  <span
                    className={`px-3 py-1 rounded-lg text-sm font-medium ${
                      user.rol === "admin"
                        ? "bg-indigo-600/30 text-indigo-300"
                        : "bg-emerald-600/30 text-emerald-300"
                    }`}
                  >
                    {user.rol}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-3">
                    <button
                      type="button"
                      className="btn btn-outline text-xs sm:text-sm"
                      onClick={() => handleValidationClick(user, "email")}
                      disabled={!user.id || !user.email || Boolean(loadingActions[`${user.id}-email`])}
                    >
                      {loadingActions[`${user.id}-email`] ? "Enviando..." : "Validar correo"}
                    </button>
                    <button
                      type="button"
                      className="btn btn-outline text-xs sm:text-sm"
                      onClick={() => handleValidationClick(user, "mobile")}
                      disabled={
                        !user.id ||
                        !(user.mobileNumber || user.telefonoMovil || user.telefono) ||
                        Boolean(loadingActions[`${user.id}-mobile`])
                      }
                    >
                      {loadingActions[`${user.id}-mobile`] ? "Enviando..." : "Validar teléfono"}
                    </button>
                  </div>
                  {feedbackMessages[user.id]?.message && (
                    <p
                      className={`mt-2 text-xs ${
                        feedbackMessages[user.id]?.variant === "error"
                          ? "text-rose-400"
                          : feedbackMessages[user.id]?.variant === "success"
                          ? "text-emerald-400"
                          : "text-gray-400"
                      }`}
                    >
                      {feedbackMessages[user.id].message}
                    </p>
                  )}
                </td>
              </motion.tr>
            ))
          )}
        </tbody>
      </table>

      {/* Paginación */}
      {totalPages > 0 && (
        <div className="flex flex-col gap-2 px-6 py-4 bg-[#2A2A2A] text-gray-300">
          <div className="flex items-center justify-between">
            <div className="text-sm">
              Mostrando <span className="text-indigo-400">{(currentPage - 1) * usersPerPage + 1}</span> - <span className="text-indigo-400">{Math.min(currentPage * usersPerPage, totalCount)}</span> de <span className="text-indigo-400">{totalCount}</span>
            </div>

            <div className="flex items-center gap-2">
              <button
                disabled={currentPage === 1}
                onClick={() => onPageChange(currentPage - 1)}
                className={`page-btn flex items-center justify-center p-1 rounded-md ${currentPage === 1 ? 'opacity-40 cursor-not-allowed' : 'text-indigo-300 hover:bg-[#333]'}`}
                aria-label="Anterior"
              >
                <ChevronLeft className="w-5 h-5" />
              </button>

              {/* page numbers: show up to 5 page numbers centered around current */}
              {Array.from({ length: totalPages }).map((_, i) => {
                const page = i + 1;
                // show if near current page
                if (Math.abs(page - currentPage) > 2 && page !== 1 && page !== totalPages) return null;
                return (
                  <button
                    key={page}
                    onClick={() => onPageChange(page)}
                    className={`page-btn px-3 py-1 rounded-md text-sm ${page === currentPage ? 'bg-indigo-600/50 text-white font-semibold scale-105' : 'text-gray-300 hover:bg-[#2f2f2f]'}`}
                    aria-current={page === currentPage ? 'page' : undefined}
                    aria-label={page === currentPage ? `Página actual ${page}` : `Ir a la página ${page}`}
                  >
                    {page}
                  </button>
                );
              })}

              <button disabled={currentPage === totalPages} onClick={() => onPageChange(currentPage + 1)} className="page-btn"><ChevronRight className="w-4 h-4" /></button>
            </div>
          </div>
        </div>
      )}
    </motion.div>
  );
};

export default UserTable;
