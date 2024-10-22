import React, { useState } from 'react';
import baseRequest from "../network/baseRequest";
import { Link, useNavigate } from 'react-router-dom';
import "./styles/Registration.css";

const Registration = () => {
  const [formData, setFormData] = useState({
    email: '',
    login: '',
    password: '',
    confirmPassword: ''
  });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState();
  const navigate = useNavigate();

  const validateForm = () => {
    const newErrors = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const loginRegex = /^[a-zA-Z0-9]+$/;
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_-])[A-Za-z\d@$!%*#?&_-]+$/;

    if (!emailRegex.test(formData.email)) {
      newErrors.email = 'Incorrect email format';
    }
    if (formData.email.length > 40) {
      newErrors.email = 'Email is too long (max size is: 40 characters)';
    }
    if (formData.login.length < 5 || formData.login.length > 15) {
      newErrors.login = 'Login must contain from 5 to 15 characters';
    }
    if (!loginRegex.test(formData.login)) {
      newErrors.login = 'Login must contain only: a-z, A-Z, 0-9';
    }
    if (formData.password.length < 9 || formData.password.length > 30) {
      newErrors.password = 'Password must contain from 9 to 30 characters';
    }
    if (!passwordRegex.test(formData.password)) {
      newErrors.password = 'Password must contain at least one uppercase letter, one lowercase letter, one number and one special character';
    }
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (validateForm()) {
      try {
        const response = await baseRequest.post('/api/user/register', {
          email: formData.email,
          login: formData.login,
          password: formData.password
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        });

        if (response.status === 201) {
          console.log('Registration successful');
          navigate('/check-email');
        }
      } catch (error) {
        if (error.response && error.response.data) {
          console.log(error);
          setErrors(error.response.data);
        } else {
          console.error('Registration error:', error);
        }
      }
    }
  };

  return (
    <div id="reg">
      <h2>Registration</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="email">Email:</label>
          <input
          className='txt1'
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
          {errors.email && <p>{errors.email}</p>}
        </div>
        <div>
          <label htmlFor="login">Login:</label>
          <input
            type="text"
            id="login"
            name="login"
            className='txt1'
            value={formData.login}
            onChange={handleChange}
            required
          />
          {errors.login && <p>{errors.login}</p>}
        </div>
        <div>
          <label htmlFor="password">Password:</label>
          <input
          className='txt1'
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
          />
          {errors.password && <p>{errors.password}</p>}
        </div>
        <div>
          <label htmlFor="confirmPassword">Repeat Password:</label>
          <input
          className='txt1'
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
          />
          {errors.confirmPassword && <p>{errors.confirmPassword}</p>}
        </div>
        <p>{message}</p>
        <button type="submit" id='sub'>Sign up</button>
      </form>
      <p>Already have an account? <Link to="/login">Login</Link></p>
    </div>
  );
};

export default Registration;