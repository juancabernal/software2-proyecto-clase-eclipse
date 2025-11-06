import axios from "axios";
import { useAuth0 } from "@auth0/auth0-react";
import { env } from "../config/env";

const AdminPage = () => {
  const { getAccessTokenSilently } = useAuth0();

  const callApi = async () => {
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: env.auth0.audience,
        },
      });
      const response = await axios.get(`${env.api.baseUrl}/api/admin`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      const responseData = response.data;

      alert(responseData.message);
    } catch (error) {
      alert(error);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center mx-96">
      <h1 className="mb-4 text-2xl font-bold">Vista Administrador.</h1>
      <div className="px-6 mb-4 text-justify">
        Esta es la vista del administrador, prueba llamando a una api exclusiva
        para usuarios administradores.
      </div>
      <button
        onClick={callApi}
        className="px-4 py-2 font-bold text-white bg-blue-500 rounded hover:bg-blue-700"
      >
        Call Admin API
      </button>
    </div>
  );
};

export default AdminPage;
